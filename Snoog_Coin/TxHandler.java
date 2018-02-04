import java.util.HashSet;
import java.util.Set;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    private UTXOPool utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */

    public boolean isValidTx(Transaction tx){
    	UTXOPool unique = new UTXOPool();
    	double input_sum = 0;
    	double output_sum = 0;
    	
    	//for(Transaction.Input inp : tx.getInputs()){
          //  UTXO utxo = new UTXO(inp.prevTxHash, inp.outputIndex);
            //Transaction.Output prev_output = utxoPool.getTxOutput(utxo);
         
    	for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input inp = tx.getInput(i);
            UTXO utxo = new UTXO(inp.prevTxHash, inp.outputIndex);
            Transaction.Output prev_output = utxoPool.getTxOutput(utxo);
            if(!utxoPool.contains(utxo)){
            	return false;}
            else if(!Crypto.verifySignature(prev_output.address, tx.getRawDataToSign(inp.outputIndex) ,inp.signature)){
            	return false;}
            else if(unique.contains(utxo)){
            	return false;}
            unique.addUTXO(utxo, prev_output);
            input_sum+=prev_output.value;
            }
            
    	for(Transaction.Output cur_out:tx.getOutputs()){
    		if(cur_out.value<0){
    			return false;
    		}
    		output_sum+=cur_out.value;
    	}
    	if(input_sum<output_sum)return false;
    	return true;
    	
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        Set<Transaction> validTxs = new HashSet<>();

    	for(Transaction tx:possibleTxs){
    		if(isValidTx(tx)){
    			validTxs.add(tx);
	    		for(Transaction.Input inp:tx.getInputs()){
	    			UTXO temp = new UTXO(inp.prevTxHash,inp.outputIndex);
	    			utxoPool.removeUTXO(temp);
	    		}
	    		for(int i = 0; i<tx.numOutputs();i++){
	    			Transaction.Output out = tx.getOutput(i);
	    			UTXO utxo = new UTXO(tx.getHash(), i);
	    			utxoPool.addUTXO(utxo, out);
	    		}
    		}
    	}
        Transaction[] validTxArray = new Transaction[validTxs.size()];
        return validTxs.toArray(validTxArray);
    }
    
    //	Extra Credit:  Create a second file called  MaxFeeTxHandler.java  whose  handleTxs()  method 
    //finds a set of transactions with maximum total transaction fees -- i.e. maximize the sum over all transactions in the set of 
    //(sum of input values - sum of output values)).

}
